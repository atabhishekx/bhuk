import PageLayout from '@/components/PageLayout'

export default function Dashboard() {
  return (
    <PageLayout title="Welcome to Bhuk" subtitle="Your Solution for all the Cravings and Snacks" cartCount={0}>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <article className="card p-6">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Featured Items</h3>
          <p className="text-gray-500 text-sm mb-4">Browse our delicious menu items.</p>
          <a href="/menu" className="btn btn-primary btn-block">View Full Menu</a>
        </article>
        <article className="card p-6">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Quick Reorder</h3>
          <p className="text-gray-500 text-sm mb-4">Reorder your favorite items.</p>
          <a href="/orders" className="btn btn-outline btn-block">View Past Orders</a>
        </article>
        <article className="card p-6">
          <h3 className="font-bold text-lg text-gray-900 mb-4">Track Delivery</h3>
          <p className="text-gray-500 text-sm mb-4">Track your active orders.</p>
          <a href="/orders" className="btn btn-outline btn-block">View Orders</a>
        </article>
      </div>
    </PageLayout>
  )
}