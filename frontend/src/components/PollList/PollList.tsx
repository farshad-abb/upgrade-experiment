import { fetchPolls } from "@/lib/serverApi";

const dateFormat = new Intl.DateTimeFormat("en", {
  dateStyle: "medium",
  timeZone: "UTC",
});

export async function PollList() {
  const polls = await fetchPolls();

  if (polls.length === 0) {
    return <p className="text-gray-600">You have no polls yet.</p>;
  }

  return (
    <ul className="flex flex-col gap-3">
      {polls.map((poll) => (
        <li
          key={poll.id}
          className="flex flex-col gap-1 rounded-xl border border-gray-200 bg-white px-4 py-3.5 shadow-sm sm:flex-row sm:items-baseline sm:justify-between sm:gap-4"
        >
          <span className="font-medium wrap-anywhere">{poll.title}</span>
          <time
            className="text-sm whitespace-nowrap text-gray-600"
            dateTime={new Date(poll.initiated).toISOString()}
          >
            {dateFormat.format(poll.initiated)}
          </time>
        </li>
      ))}
    </ul>
  );
}

export function PollListSkeleton() {
  return (
    <p className="text-gray-600" role="status">
      Loading polls…
    </p>
  );
}
