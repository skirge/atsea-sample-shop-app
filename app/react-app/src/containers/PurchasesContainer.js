import React, { Component } from 'react'
import { Link } from 'react-router'
import { getJwtToken } from '../actions/storage'
import './PurchasesContainer.css'

const request = require('superagent-promise')(require('superagent'), Promise)

class PurchasesContainer extends Component {
  state = { purchases: [], loading: true, error: null }

  componentDidMount() {
    const token = getJwtToken()
    if (!token) {
      this.setState({ loading: false, error: 'Please sign in at the store to view your purchases.' })
      return
    }
    request.get('/purchase/')
      .set('Authorization', 'Bearer ' + token)
      .accept('application/json')
      .end()
      .then(response => {
        if (!this.unmounted) {
          this.setState({ purchases: response.body, loading: false })
        }
      })
      .catch(() => {
        if (!this.unmounted) {
          this.setState({ loading: false, error: 'Unable to load purchases. Please sign in at the store and try again.' })
        }
      })
  }

  componentWillUnmount() {
    this.unmounted = true
  }

  // Intentional dojo self-XSS: the customer's own stored fields become raw HTML.
  renderCustomerField(value) {
    return <div dangerouslySetInnerHTML={{ __html: value || '' }} />
  }

  render() {
    const { purchases, loading, error } = this.state
    return (
      <main className="customerPurchases">
        <h1>My Purchases</h1>
        <p><Link to="/">Back to store</Link></p>
        {loading && <p>Loading purchases...</p>}
        {error && <p role="alert">{error}</p>}
        {!loading && !error && (purchases.length === 0 ? <p>No purchases yet.</p> :
          <div className="customerPurchaseTable">
            <table>
              <thead>
                <tr><th>Purchase</th><th>Date</th><th>Name</th><th>Company / title</th><th>Billing address</th><th>Products (ID: quantity)</th></tr>
              </thead>
              <tbody>
                {purchases.map(purchase => (
                  <tr key={purchase.purchaseId}>
                    <td>{purchase.purchaseId}</td>
                    <td>{new Date(purchase.purchasedAt).toLocaleString()}</td>
                    <td>{this.renderCustomerField(purchase.firstName)}{this.renderCustomerField(purchase.lastName)}</td>
                    <td>{this.renderCustomerField(purchase.company)}{this.renderCustomerField(purchase.title)}</td>
                    <td>{this.renderCustomerField(purchase.address)}{this.renderCustomerField(purchase.city)}</td>
                    <td>{JSON.stringify(purchase.productsOrdered)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    )
  }
}

export default PurchasesContainer
