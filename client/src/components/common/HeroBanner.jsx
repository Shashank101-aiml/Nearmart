export default function HeroBanner() {
  return (
    <div className="mt-4 overflow-hidden rounded-3xl bg-gradient-to-br from-accent-dark via-accent to-accent-hover px-6 py-10 text-white sm:px-10 sm:py-14">
      <div className="flex flex-col items-start gap-10 lg:flex-row lg:items-center lg:justify-between">
        <div className="max-w-xl">
          <span className="inline-flex items-center gap-2 rounded-full bg-white/15 px-3.5 py-1.5 text-xs font-black tracking-wide uppercase">
            <span className="h-1.5 w-1.5 rounded-full bg-white" />
            Nearmart Express &bull; Guaranteed 15 mins
          </span>
          <h1 className="m-0 mt-4 text-4xl leading-tight font-black sm:text-5xl">
            Everything Delivered in{' '}
            <span className="[text-decoration-color:rgba(255,255,255,0.6)] underline decoration-wavy decoration-4 underline-offset-8">
              15 Minutes
            </span>
          </h1>
          <p className="mt-5 max-w-md text-sm text-white/85 sm:text-base">
            Order organic farm produce, everyday groceries, snacks, and medicine &mdash; delivered to your door
            instantly.
          </p>
          <a
            href="#catalog"
            className="mt-7 inline-flex items-center gap-2 rounded-xl bg-white px-5 py-3 text-sm font-bold text-accent shadow-sm hover:bg-white/90"
          >
            Shop Express
            <i className="fa-solid fa-arrow-right" />
          </a>
        </div>

        <div className="w-full max-w-xs rounded-2xl bg-white/15 p-6 backdrop-blur-sm">
          <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-white/20">
            <i className="fa-solid fa-bolt text-xl" />
          </div>
          <h2 className="m-0 mt-4 text-lg font-black">Ultra-Fast Dispatch</h2>
          <p className="mt-1 text-xs text-white/80">Over 100,000+ orders delivered this month</p>
          <div className="mt-5 grid grid-cols-3 gap-2 border-t border-white/20 pt-4 text-center">
            <div>
              <p className="m-0 text-lg font-black">15m</p>
              <p className="m-0 text-[10px] text-white/70 uppercase">Speed</p>
            </div>
            <div>
              <p className="m-0 text-lg font-black">$0</p>
              <p className="m-0 text-[10px] text-white/70 uppercase">Above $199</p>
            </div>
            <div>
              <p className="m-0 text-lg font-black">4.95&#9733;</p>
              <p className="m-0 text-[10px] text-white/70 uppercase">Rating</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
