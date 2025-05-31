import dayjs from 'dayjs'
import utc from 'dayjs/plugin/utc'
import relativeTime from 'dayjs/plugin/relativeTime'
import type { Timestamp } from '@bufbuild/protobuf/wkt'

export function throttle<T extends (...args: any[]) => any>(
  fn: T,
  delay: number,
): (...args: Parameters<T>) => void {
  let lastCall = 0
  return function (...args: Parameters<T>) {
    const now = new Date().getTime()
    if (now - lastCall >= delay) {
      lastCall = now
      fn(...args)
    }
  }
}

export async function countAsyncIterable<T>(
  iterable: AsyncIterable<T> | undefined,
): Promise<number> {
  let count = 0
  if (!iterable) return count
  for await (const _ of iterable) {
    count++
  }
  return count
}

/**
 * Count time from a date
 */

dayjs.extend(relativeTime)
dayjs.extend(utc)

/**
 *
 * @param date : 'yyyy-mm-ddThh:mm:ss'
 * @returns
 */
export function timeAgo(mydate: Timestamp): string {
  const dateInput = Number(mydate.seconds) * 1000
  const targetDate = dayjs.utc(dateInput)
  return targetDate.fromNow()
}


export function timestampToDateSafe(ts?: { seconds?: bigint; nanos?: number }): Date {
  const seconds = ts?.seconds ?? 0n
  const nanos = ts?.nanos ?? 0
  return new Date(Number(seconds) * 1000 + nanos / 1e6)
}
