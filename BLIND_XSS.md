# Blind XSS and self-XSS in purchase review

Checkout sends name, company, title, billing address, city, and product quantities
to `POST /purchase/` with the signed-in customer's Bearer token. The application
stores a snapshot in `purchases` and `purchasequantities`. Existing Hibernate
schema management creates these tables; profiles using `create-drop` erase them
on shutdown, while the PostgreSQL/MySQL profiles use `update`.

The customer receives a confirmation without their submitted HTML. An admin
later visits `/admin/purchases`, where Thymeleaf `th:utext` renders those saved
fields without escaping. This is the intentional blind XSS sink.

## Exercise

1. Sign in as a customer, add a product to the cart, and open checkout.
2. Put this harmless demonstration payload in the **Company** field:

   ```html
   <img src=x onerror="document.title='Blind XSS triggered'">
   ```

3. Complete the order. The customer sees the usual success message.
4. In a separate browser session, open `/admin/purchases`. At the HTTP Basic
   prompt, enter username `admin` and password `admin123!`.
   The page title changes to `Blind XSS triggered` when the stored image fails
   to load. Reloading the page triggers the payload again.

On startup, the application creates the enabled `admin` account with role `ADMIN`
if that username does not exist. Subsequent starts preserve the existing account,
including any changed password. Customers continue to register through the
storefront's **Create User** flow.

Ordinary customers receive HTTP 403 on the list. Anonymous requests receive HTTP
401. The existing login request's client-controlled `admin` flag does not grant
access; this page checks the database authority through Spring Security. Other
deliberate account-management and JWT weaknesses in the dojo remain present.

The form's card number, CVV, and expiration date are excluded from the purchase
request and persistence model.

## Customer self-XSS exercise

After completing a purchase containing the image payload above, return to the
store and choose **My Purchases**, or open `http://localhost:8080/#/purchases`
while signed in as that customer. The saved name and billing fields are inserted
as HTML with React's `dangerouslySetInnerHTML`. The payload changes the customer's
page title when the image fails to load, demonstrating stored self-XSS. Reloading
the page triggers it again.

This page uses the storefront's existing Bearer token. Its `GET /purchase/`
request returns only purchases associated with that token's customer account,
newest first. Other customers' purchases are excluded. The same stored payload
still triggers blind XSS if an admin opens the admin purchase list.

## Validation

With Java 8 and the Maven dependencies available locally:

```sh
cd app
mvn -o -Dtest=BlindXssPurchaseTest,ResourceControllerTest test
```

The integration tests use H2, the real security configuration and JWT filter,
JPA repositories, and Thymeleaf rendering. They check stored HTML, customer
ownership, cart validation, and admin access. Browser script execution is a
manual step in the exercise above.
