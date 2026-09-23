import CompanyPageBanner from '../../components/common/CompanyPageBanner'

const TOPICS = [
  { title: 'Track an order', body: 'See live status from "Placed" to "Delivered" on the Orders page.' },
  { title: 'Payments & refunds', body: 'Failed or double payments are usually reversed within 5-7 business days.' },
  { title: 'Delivery issues', body: 'Missing or damaged items? Let us know within 24 hours of delivery.' },
  { title: 'Account & login', body: 'Trouble signing in, or want to update your phone number or address?' },
]

export default function HelpSupportPage() {
  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <CompanyPageBanner />

      <div className="mx-auto mt-12 max-w-3xl">
        <h1 className="m-0 mb-4 text-[32px] text-text-h">
          We would <span aria-hidden="true">&#10084;&#65039;</span> to hear from you
        </h1>
        <p className="mb-8 leading-relaxed text-text">
          Whether it&rsquo;s a question about an order, a payment that didn&rsquo;t go through, or just feedback on
          how we&rsquo;re doing &mdash; we want to hear it.
        </p>

        <div className="rounded-2xl border border-border bg-bg p-6">
          <p className="m-0 leading-relaxed text-text-h">
            For any queries or assistance, feel free to reach out to us at{' '}
            <a href="mailto:support@nearmart.com" className="font-semibold text-accent hover:underline">
              support@nearmart.com
            </a>
            . We typically respond within a few hours.
          </p>
        </div>

        <h2 className="mt-10 mb-4 text-xl text-text-h">Common topics</h2>
        <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
          {TOPICS.map((topic) => (
            <div
              key={topic.title}
              className="rounded-xl border border-border bg-bg p-4 transition-transform duration-200 ease-out hover:-translate-y-1 hover:shadow-lg"
            >
              <h3 className="m-0 mb-1 text-sm font-black text-text-h">{topic.title}</h3>
              <p className="m-0 text-sm leading-relaxed text-text">{topic.body}</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
