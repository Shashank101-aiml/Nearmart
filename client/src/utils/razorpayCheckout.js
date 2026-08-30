import * as paymentService from '../services/paymentService'

export function openRazorpayCheckout(order, { onSettled }) {
  const options = {
    key: order.razorpayKeyId,
    amount: order.amountInPaise,
    currency: 'INR',
    order_id: order.razorpayOrderId,
    name: 'Nearmart',
    handler: async (response) => {
      try {
        await paymentService.verifyPayment(order.id, {
          razorpayOrderId: response.razorpay_order_id,
          razorpayPaymentId: response.razorpay_payment_id,
          razorpaySignature: response.razorpay_signature,
        })
      } catch {
        // Verification failed server-side; the order is now PAYMENT_FAILED and retryable
        // from the Orders page — nothing more to do here.
      } finally {
        onSettled()
      }
    },
    modal: {
      ondismiss: () => onSettled(),
    },
  }

  new window.Razorpay(options).open()
}
