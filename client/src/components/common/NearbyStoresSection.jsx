import { NEARBY_STORE_GROUPS } from '../../constants/nearbyStores'

export default function NearbyStoresSection() {
  return (
    <div className="mt-12">
      <h2 className="m-0 mb-1 text-[22px] text-text-h">More Stores Near You</h2>
      <p className="mt-0 mb-6 text-sm text-text">Coming soon to Nearmart &mdash; browse what's on the way.</p>

      <div className="flex flex-col gap-8">
        {NEARBY_STORE_GROUPS.map((group) => (
          <div key={group.category}>
            <h3 className="m-0 mb-3 text-base font-bold text-text-h">
              {group.icon} {group.category}
            </h3>
            <div className="grid grid-cols-[repeat(auto-fill,minmax(240px,1fr))] gap-4">
              {group.stores.map((store) => (
                <div
                  key={store.name}
                  className="relative flex flex-col gap-1.5 rounded-lg border border-border bg-bg p-4 opacity-90"
                >
                  <span className="absolute top-3 right-3 rounded-full bg-code-bg px-2 py-0.5 text-[10px] font-bold text-text uppercase">
                    Coming soon
                  </span>
                  <h4 className="m-0 pr-20 text-base text-text-h">{store.name}</h4>
                  <p className="text-xs text-text">
                    {store.location} &middot; {store.distanceKm} km away
                  </p>
                  <p className="text-xs text-text">{store.description}</p>
                  <p className="mt-1 text-xs font-semibold text-text-h">
                    ETA: {store.etaMinutes} mins &nbsp;|&nbsp; Rating: {store.rating}&#9733; (
                    {store.reviewCount.toLocaleString()} reviews)
                  </p>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
