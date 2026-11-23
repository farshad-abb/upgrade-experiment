# Upgrade button A/B test

A minimal poll list dashboard with an "Upgrade" button that runs as an A/B test.
The backend (Spring Boot) returns polls list, assigns visitors to a version and counts views and clicks.
The frontend (Next.js) shows the polls and the visitor's version of the button and sends the tracking events to backend

## How to start

You need Java 21 and Node.js 20 or newer. Use two terminals.

```bash
# Terminal 1: backend on http://localhost:8080
cd backend
./mvnw spring-boot:run

# Terminal 2: frontend on http://localhost:3000
cd frontend
npm install
npm run dev
```

See the results at http://localhost:3000/api/experiments/upgrade-button/results.

### Tests

```bash
cd backend && ./mvnw test     # split, stickiness, events, results, polls
cd frontend && npm test       # button shows the version, records seen and click, keyboard; visitor cookie
```

## API

| Method | Path                                       | Response                                                                                     |
| ------ | ------------------------------------------ | -------------------------------------------------------------------------------------------- |
| GET    | `/api/polls`                               | `200 [{ "id", "title", "initiated" }]`: all polls, newest first                              |
| GET    | `/api/visitors/{visitorId}/assignments`    | `200 { "upgrade-button": "B" }`: assigns on the first call, same answer after                |
| POST   | `/api/events`                              | `204`. Body: `{ "event", "experimentKey"? }`, visitor from the `visitor_id` cookie           |
| GET    | `/api/experiments/{experimentKey}/results` | `200 { "experimentKey", "variants": { "A": { "seen", "clicked", "clickRate" }, "B": {…} } }` |

- The visitor comes only from the `visitor_id` cookie, so a caller can only record events for itself. A
  missing cookie or one that is not a UUID gives `400`. Only JSON bodies are accepted (`415` otherwise).
- Events (`button_seen`, `button_clicked`) need `experimentKey`. `400` when the event or experiment is
  unknown, the event does not belong to the experiment, or the visitor has no assignment.
- The client never sends its variant. The server looks it up, so a wrong or edited client cannot put a
  visitor in the wrong group.
- Errors are [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) problem JSON: `{ "status": 400, "detail": "…" }`.
- experimentKey is used in endpoints to make API scalable for running multiple A/B tests in the future. Unknown experiment on results in `404`.

## Design decisions

- **Visitor id:** Next sets an httpOnly visitorId cookie and the page is rendered on the server with the selected variant, so there's no flicker. Server cookie + SSR approach is used.
- **Assignment:** in memory on the server , 50/50 with `SecureRandom`.
  Assignments are lost on restart, which the challenge allows. For production, persistant assignments is prefered which is not implemented here for simplicity.
- **Variant rendered on the server:** the button is a server component that reads the cookie and asks the
  backend for the variant while rendering. The browser never caches the variant, so it cannot disagree with what
  the server counts. The button sits in its own `<Suspense>`: the rest of the page stays a static shell and
  the button streams into the same response.
- **Counting once:** events are stored as `visitorId → first time seen` per variant and event, so reloads
  and double clicks are stored once and every count is unique visitors. A click only counts if the same
  visitor was also seen, so the click rate can never be above 1. The results show both versions even with
  no visitors (`clickRate` is 0 then).
- **"Seen"** is sent from the browser after the page has loaded and the visitor's version is on screen
  (not while rendering on the server, so prefetches and bots are not counted). If the backend is
  down the page shows version A and records nothing, so visitors without an assignment never reach the
  results.
- **Page speed:** the header and button slot are a static shell that is sent at once; the button and the
  poll list are rendered on the server and streamed in.

### How this could grow

- **Stateless assignment at scale:** replace the stored assignments with a deterministic hash
  `hash(experimentKey + visitorId) % 2` and serve one cached page per version (we could use Vercel Flags SDK and
  "precompute") or tools like GrowthBook that do the same in their SDK.
- **Sticky across changes:** store assignments (Redis/DB) or use "sticky bucketing", so changing the split
  later does not move existing visitors.
- **Persistence:** keep events in a database or event pipeline instead of memory.

## Questions/Answers

**1. What does version B change, and why do you think it will get more clicks?**

B changes only the text: "Upgrade" becomes "Get unlimited polls". "Upgrade" asks the user to pay but doesn't say what the paid plan gives them, so the value proposition is missing. The main user pain point for free users is the poll limit. Version B names that benefit directly, at the moment the user is looking at their polls. I expect this to increase motivation to click.

**2. How many visitors would you want before you trust the result? How did you get to that number?**

Let's assume the Base convertion rate(currrent click rate on "update" button) is 5% and Minimum detectable effect (MDE) is 20% relative(6% click rate for new button). And if we consider usual settings (5%
significance, 80% power) then based on standard two-proportion sample size formula, we need approximately 8,000 visitors per version, or 16,000 visitors in total

**3. The split should be close to 50/50. What could make it come out different, and how would you notice?**

Small random differences(for example 51% A and 49% B) are normal. Bigger ones should be investigated, they could point to a bug, for example: one version fails to
render in some cases or BE is not excluding double clicks; or server reloads reassigns variants and so on ...
we can have sample ratio mismatch check based on the result of "results" endpoint every few days and investiage the result. A common formula like chi-square test could be used with alerting when p < 0.001.

**4. A click on "Upgrade" is not a subscription. What would you measure to know whether version B brings more paid subscriptions?**

We need to record a tacking event that shows which number of users clicked on button, converted to the paid subcription ar the end and then compare which variant performs better. A tracking event like `subscription_paid` that triggers on successful payment and is linked with visitorId to make the calculation possible.
Record the payment as an event of the same visitor (`subscription_paid`, not implemented here),
and link the visitor id to the account when they log in or sign up. Then compare per version: paid
subscriptions per visitor who saw the button, revenue per visitor, and which plans they choose.

## Time and scope

- Time spent: around 3 hours
- Code cleanup, E2E tests, persistant assignments, scalable solution, ...
- AI tools:
  Scaffolding: the Spring Boot app, the Next.js + Tailwind setup, and the Vitest + Testing Library setup
  Boilerplate such as the RFC 9457 problem-details error handler and the DTOs and controllers
  Test generation for both backend and frontend
  Checking the sample-size math in Q2 (two-proportion formula, ~8k per version)
  Polishing Readme file
