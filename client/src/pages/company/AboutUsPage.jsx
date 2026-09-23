import HeroBanner from '../../components/common/HeroBanner'

export default function AboutUsPage() {
  return (
    <div className="flex-1 w-full max-w-7xl mx-auto px-8 pt-6 pb-12 text-left">
      <HeroBanner />

      <div className="mx-auto mt-12 max-w-3xl">
        <h1 className="m-0 mb-4 text-[32px] text-text-h">About Nearmart</h1>
        <p className="mb-4 leading-relaxed text-text">
          Nearmart is a hyper-local quick-commerce platform built to close the gap between your nearest
          neighbourhood store and your doorstep. We believe getting fresh groceries, everyday essentials, and
          medicines shouldn&rsquo;t mean planning a trip or waiting a day &mdash; it should take minutes.
        </p>
        <p className="mb-4 leading-relaxed text-text">
          We connect three groups of people who make that possible: local vendors who stock the products, delivery
          partners who carry them the last mile, and customers who need them fast. Every order on Nearmart is
          fulfilled by a real neighbourhood store near you, not a distant warehouse &mdash; which means fresher
          produce, faster delivery, and more support for local businesses.
        </p>
        <p className="mb-4 leading-relaxed text-text">
          Since launching, we&rsquo;ve grown into a network of vendors spanning groceries, dairy, pharmacy, meat and
          fish, household essentials, and more &mdash; all searchable from one app, all delivered by partners who
          know the neighbourhood. Our promise is simple: what you need, from someone nearby, in about 15 minutes.
        </p>
        <p className="leading-relaxed text-text">
          We&rsquo;re just getting started. As more vendors and delivery partners join Nearmart, we&rsquo;re able to
          reach more neighbourhoods, stock more products, and deliver even faster &mdash; and we&rsquo;re glad you&rsquo;re
          here for it.
        </p>
      </div>
    </div>
  )
}
