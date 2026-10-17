import React, { Component, PropTypes } from 'react'
import { connect } from 'react-redux'
import {
  checkout,
  purchaseOrder,
} from '../actions'
import { getTotal, getCartProducts, getTotalProducts, getQuantityById } from '../reducers'
import SuccessMessage from '../components/SuccessMessage'
import Checkout from '../components/Checkout'
import { Link } from 'react-router'
import { SubmissionError } from 'redux-form'


class CheckoutContainer extends Component {
  constructor(props) {
    super(props)
    this.state = {
      orderComplete: false,
    }
  }

  handleSuccess = () => {
    this.setState({ orderComplete: true })
  }

  handleSubmit = (values) => {
    const {
      purchaseOrder,
      totalProducts,
      quantityById
    } = this.props

    const {
      firstName,
      lastName,
      company,
      title,
      address,
      city,
    } = values
    const submitData = {
      firstName,
      lastName,
      company,
      title,
      address,
      city,
      productsOrdered: quantityById
    }

    if (totalProducts === 0) {
      throw new SubmissionError({ _error: "Please add to cart first..." })
    }

    return purchaseOrder(submitData)
      .then(this.handleSuccess)
      .catch(() => {
        throw new SubmissionError({ _error: "Unable to save your purchase. Please sign in and try again." })
      })
  }

  renderCheckout() {
    const { products, total, totalProducts, checkout } = this.props
    return (
      <Checkout
        products={products}
        total={total}
        totalProducts={totalProducts}
        onCheckoutClicked={() => checkout(products)}
        handleSubmit={this.handleSubmit}
      />
    );
  }

  renderSuccess() {
    const successMessage = "You have successfully placed an order!"
    return (
      <SuccessMessage
        message={successMessage}
        label="Continue Shopping"
        containerElement={<Link to="/" />}
      />
    );
  }

  render() {
    return (
      <div>
        {this.state.orderComplete ?
          this.renderSuccess()
          : this.renderCheckout()
        }
      </div>
    );
  }
}

CheckoutContainer.propTypes = {
  products: PropTypes.arrayOf(PropTypes.shape({
    productId: PropTypes.number.isRequired,
    name: PropTypes.string.isRequired,
    price: PropTypes.number.isRequired,
    quantity: PropTypes.number.isRequired,
    image: PropTypes.string.isRequired,
  })).isRequired,
  total: PropTypes.string,
  checkout: PropTypes.func.isRequired
}

const mapStateToProps = (state) => ({
  products: getCartProducts(state),
  total: getTotal(state),
  totalProducts: getTotalProducts(state),
  quantityById: getQuantityById(state),
})

export default connect(
  mapStateToProps,
  { checkout, purchaseOrder }
)(CheckoutContainer)
