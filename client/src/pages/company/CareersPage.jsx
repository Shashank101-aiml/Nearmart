import CompanyPageBanner from '../../components/common/CompanyPageBanner'

export default function CareersPage() {
  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <CompanyPageBanner />

      <div className="mx-auto mt-12 max-w-3xl">
        <h1 className="m-0 mb-4 text-[32px] text-text-h">Careers at Nearmart</h1>
        <p className="mb-4 leading-relaxed text-text">
          There are no open positions right now &mdash; but we&rsquo;re growing, and that changes quickly. New roles
          across engineering, operations, and city expansion get posted here as soon as they open up.
        </p>
        <p className="leading-relaxed text-text">
          If building a faster, more local way to shop sounds like your kind of problem to solve, check back
          regularly &mdash; we&rsquo;d love to have you on the team when the right opening comes along.
        </p>
      </div>
    </div>
  )
}
