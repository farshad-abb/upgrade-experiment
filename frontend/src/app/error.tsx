"use client";

export default function Error({ reset }: { error: Error; reset: () => void }) {
  return (
    <main className="mx-auto max-w-3xl px-4 py-6">
      <h1 className="mb-2 text-2xl font-bold">Something went wrong</h1>
      <p className="mb-4 text-gray-600">Unable to load your polls.</p>
      <button
        type="button"
        onClick={reset}
        className="cursor-pointer rounded-full border border-gray-200 bg-white px-4 py-2 font-semibold hover:bg-gray-50 focus-visible:outline-3 focus-visible:outline-offset-2 focus-visible:outline-amber-500"
      >
        Try again
      </button>
    </main>
  );
}
