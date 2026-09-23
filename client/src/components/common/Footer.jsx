import { Link, useNavigate } from 'react-router-dom'
import { PRODUCT_CATEGORIES } from '../../constants/categories'

const FOOTER_CATEGORIES = ['VEGETABLES', 'DAIRY_EGGS', 'COLD_DRINKS', 'PHARMACY']

export default function Footer() {
  const navigate = useNavigate()

  return (
    <footer className="mt-16 border-t border-border bg-bg py-12">
      <div className="mx-auto grid w-full max-w-7xl grid-cols-2 gap-8 px-8 text-left md:grid-cols-5">
        <div className="col-span-2 flex flex-col gap-3">
          <div className="flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-accent text-lg font-black text-white">
              N
            </div>
            <span className="text-xl font-black text-text-h">nearmart</span>
          </div>
          <p className="max-w-sm text-xs leading-relaxed text-text">
            Nearmart is your hyper-local 15-minute quick-commerce delivery platform. Get daily fresh groceries,
            medicines, and essentials from local vendors near you.
          </p>
        </div>

        <div>
          <h4 className="mb-3 text-xs font-black tracking-wider text-text-h uppercase">Categories</h4>
          <ul className="flex flex-col gap-2 text-xs font-semibold text-text">
            {FOOTER_CATEGORIES.map((value) => {
              const category = PRODUCT_CATEGORIES.find((c) => c.value === value)
              return (
                <li key={value}>
                  <button
                    type="button"
                    onClick={() => navigate(`/customer?category=${value}`)}
                    className="cursor-pointer border-none bg-transparent p-0 text-left font-semibold hover:text-accent"
                  >
                    {category.label}
                  </button>
                </li>
              )
            })}
          </ul>
        </div>

        <div>
          <h4 className="mb-3 text-xs font-black tracking-wider text-text-h uppercase">Company</h4>
          <ul className="flex flex-col gap-2 text-xs font-semibold text-text">
            <li>
              <Link to="/about" className="hover:text-accent">
                About Us
              </Link>
            </li>
            <li>
              <Link to="/careers" className="hover:text-accent">
                Careers
              </Link>
            </li>
            <li>
              <Link to="/register?role=VENDOR" className="hover:text-accent">
                Nearmart Partner
              </Link>
            </li>
          </ul>
        </div>

        <div>
          <h4 className="mb-3 text-xs font-black tracking-wider text-text-h uppercase">Contact</h4>
          <ul className="flex flex-col gap-2 text-xs font-semibold text-text">
            <li>
              <a href="#" className="hover:text-accent">
                Help &amp; Support
              </a>
            </li>
            <li>
              <a href="#" className="hover:text-accent">
                Terms of Service
              </a>
            </li>
          </ul>
        </div>
      </div>

      <div className="mx-auto mt-8 flex w-full max-w-7xl flex-col items-center justify-between gap-4 border-t border-border px-8 pt-6 text-xs font-medium text-text sm:flex-row">
        <p className="m-0">&copy; 2026 Nearmart Technologies Pvt. Ltd. All rights reserved.</p>
        <div className="flex gap-4 text-base text-text">
          <a href="#" className="hover:text-accent" aria-label="Instagram">
            <i className="fa-brands fa-instagram" />
          </a>
          <a href="#" className="hover:text-accent" aria-label="Twitter">
            <i className="fa-brands fa-twitter" />
          </a>
          <a href="#" className="hover:text-accent" aria-label="Facebook">
            <i className="fa-brands fa-facebook" />
          </a>
        </div>
      </div>
    </footer>
  )
}
