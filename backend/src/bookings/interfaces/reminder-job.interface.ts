export interface BookingReminderJob {
  bookingId: string;
  userId: string;
  minutesBefore: 60 | 15;
}
