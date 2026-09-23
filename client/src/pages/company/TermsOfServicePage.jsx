import CompanyPageBanner from '../../components/common/CompanyPageBanner'

const SECTIONS = [
  {
    title: '1. Acceptance of Terms',
    body: 'By creating an account or placing an order on Nearmart, you agree to be bound by these Terms of Service. If you do not agree, please do not use the platform.',
  },
  {
    title: '2. Accounts',
    body: 'You are responsible for maintaining the confidentiality of your login credentials and for all activity under your account. Provide accurate information when registering as a customer, vendor, or delivery partner.',
  },
  {
    title: '3. Orders, Pricing & Payments',
    body: 'Prices shown at checkout are final for that order. Payments are processed through our third-party payment gateway; Nearmart does not store your card or bank details. We reserve the right to cancel an order in cases of pricing errors, stock unavailability, or suspected fraud.',
  },
  {
    title: '4. Delivery',
    body: 'Delivery windows are estimates, not guarantees, and may vary with demand, weather, or local conditions. Someone must be available to receive the order at the delivery address provided.',
  },
  {
    title: '5. Returns, Refunds & Cancellations',
    body: 'Perishable and prepared items are generally non-returnable once delivered. Damaged, missing, or incorrect items should be reported within 24 hours of delivery for a replacement or refund. Approved refunds are credited to the original payment method within 5-7 business days.',
  },
  {
    title: '6. Vendor & Delivery Partner Obligations',
    body: 'Vendors are responsible for the accuracy, quality, and safety of the products they list. Delivery partners are independent participants who accept and fulfil deliveries through the app; Nearmart is not a party to the sale between a vendor and a customer.',
  },
  {
    title: '7. Prohibited Conduct',
    body: 'You may not use Nearmart to violate any law, infringe another party’s rights, upload false or misleading listings, or interfere with the normal operation of the platform.',
  },
  {
    title: '8. Intellectual Property',
    body: 'The Nearmart name, logo, and app design are the property of Nearmart Technologies Pvt. Ltd. and may not be used without permission.',
  },
  {
    title: '9. Limitation of Liability',
    body: 'Nearmart is provided on an "as is" basis. To the fullest extent permitted by law, Nearmart is not liable for indirect or consequential damages arising from your use of the platform.',
  },
  {
    title: '10. Changes to These Terms',
    body: 'We may update these Terms from time to time. Continued use of Nearmart after changes are posted constitutes acceptance of the revised Terms.',
  },
  {
    title: '11. Governing Law',
    body: 'These Terms are governed by the laws of India, without regard to conflict-of-law principles.',
  },
  {
    title: '12. Contact Us',
    body: 'Questions about these Terms can be sent to support@nearmart.com.',
  },
]

export default function TermsOfServicePage() {
  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <CompanyPageBanner />

      <div className="mx-auto mt-12 max-w-3xl">
        <h1 className="m-0 mb-2 text-[32px] text-text-h">Terms of Service</h1>
        <p className="mb-8 text-sm text-text">Last updated: September 23, 2026</p>

        <div className="flex flex-col gap-6">
          {SECTIONS.map((section) => (
            <div key={section.title}>
              <h2 className="m-0 mb-2 text-lg text-text-h">{section.title}</h2>
              <p className="m-0 leading-relaxed text-text">{section.body}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
