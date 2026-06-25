import {
  boolean,
  doublePrecision,
  integer,
  pgEnum,
  pgTable,
  text,
  timestamp,
} from "drizzle-orm/pg-core";

// ── Auth tables (Better Auth managed) ──────────────────────────────────────

export const user = pgTable("user", {
  id: text("id").primaryKey(),
  name: text("name").notNull(),
  email: text("email").notNull().unique(),
  emailVerified: boolean("email_verified").notNull(),
  image: text("image"),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
});

export const session = pgTable("session", {
  id: text("id").primaryKey(),
  expiresAt: timestamp("expires_at").notNull(),
  token: text("token").notNull().unique(),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
  ipAddress: text("ip_address"),
  userAgent: text("user_agent"),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
});

export const account = pgTable("account", {
  id: text("id").primaryKey(),
  accountId: text("account_id").notNull(),
  providerId: text("provider_id").notNull(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
  accessToken: text("access_token"),
  refreshToken: text("refresh_token"),
  idToken: text("id_token"),
  accessTokenExpiresAt: timestamp("access_token_expires_at"),
  refreshTokenExpiresAt: timestamp("refresh_token_expires_at"),
  scope: text("scope"),
  password: text("password"),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
});

export const verification = pgTable("verification", {
  id: text("id").primaryKey(),
  identifier: text("identifier").notNull(),
  value: text("value").notNull(),
  expiresAt: timestamp("expires_at").notNull(),
  createdAt: timestamp("created_at"),
  updatedAt: timestamp("updated_at"),
});

// ── App enums ───────────────────────────────────────────────────────────────

export const genderEnum = pgEnum("gender", ["MALE", "FEMALE", "OTHER"]);
export const studentStatusEnum = pgEnum("student_status", [
  "ACTIVE",
  "COMPLETED",
  "DROPPED",
  "ON_LEAVE",
]);
export const durationUnitEnum = pgEnum("duration_unit", ["MONTH", "YEAR"]);
export const paymentModeEnum = pgEnum("payment_mode", [
  "CASH",
  "ONLINE",
  "CHEQUE",
  "DD",
  "UPI",
  "CARD",
]);
export const paymentStatusEnum = pgEnum("payment_status", [
  "COMPLETED",
  "PENDING",
  "FAILED",
]);

export const attendanceStatusEnum = pgEnum("attendance_status", [
  "PRESENT",
  "ABSENT",
  "LATE",
  "HALF_DAY",
]);

// ── App tables ──────────────────────────────────────────────────────────────

export const course = pgTable("course", {
  id: text("id").primaryKey(),
  name: text("name").notNull(),
  code: text("code").notNull().unique(),
  description: text("description"),
  durationValue: text("duration_value").notNull(),
  durationUnit: durationUnitEnum("duration_unit").notNull(),
  feeAmount: doublePrecision("fee_amount").notNull(),
  isActive: boolean("is_active").notNull().default(true),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").notNull().defaultNow(),
});

export const student = pgTable("student", {
  id: text("id").primaryKey(),
  enrollmentNumber: text("enrollment_number").notNull().unique(),
  firstName: text("first_name").notNull(),
  lastName: text("last_name").notNull(),
  fatherName: text("father_name").notNull(),
  motherName: text("mother_name").notNull(),
  dateOfBirth: text("date_of_birth").notNull(),
  gender: genderEnum("gender").notNull(),
  email: text("email"),
  phone: text("phone").notNull(),
  alternatePhone: text("alternate_phone"),
  addressStreet: text("address_street").notNull(),
  addressCity: text("address_city").notNull(),
  addressState: text("address_state").notNull(),
  addressPinCode: text("address_pin_code").notNull(),
  courseId: text("course_id")
    .notNull()
    .references(() => course.id),
  enrollmentDate: text("enrollment_date").notNull(),
  status: studentStatusEnum("status").notNull().default("ACTIVE"),
  photoUrl: text("photo_url"),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").notNull().defaultNow(),
});

export const attendance = pgTable("attendance", {
  id: text("id").primaryKey(),
  studentId: text("student_id")
    .notNull()
    .references(() => student.id),
  date: text("date").notNull(),
  status: attendanceStatusEnum("status").notNull(),
  note: text("note"),
  createdAt: timestamp("created_at").notNull().defaultNow(),
});

// ── Forms ───────────────────────────────────────────────────────────────────

export const fieldTypeEnum = pgEnum("field_type", [
  "TEXT", "PARAGRAPH", "NUMBER", "DATE", "DROPDOWN", "CHECKBOX", "RADIO",
]);

export const formTemplate = pgTable("form_template", {
  id: text("id").primaryKey(),
  title: text("title").notNull(),
  description: text("description").notNull().default(""),
  createdAt: timestamp("created_at").notNull().defaultNow(),
  updatedAt: timestamp("updated_at").notNull().defaultNow(),
});

export const formField = pgTable("form_field", {
  id: text("id").primaryKey(),
  formId: text("form_id").notNull().references(() => formTemplate.id, { onDelete: "cascade" }),
  label: text("label").notNull(),
  type: fieldTypeEnum("type").notNull(),
  required: boolean("required").notNull().default(false),
  options: text("options").notNull().default("[]"),
  order: integer("order").notNull().default(0),
});

export const formResponse = pgTable("form_response", {
  id: text("id").primaryKey(),
  formId: text("form_id").notNull().references(() => formTemplate.id, { onDelete: "cascade" }),
  submittedAt: text("submitted_at").notNull(),
  createdAt: timestamp("created_at").notNull().defaultNow(),
});

export const formAnswer = pgTable("form_answer", {
  id: text("id").primaryKey(),
  responseId: text("response_id").notNull().references(() => formResponse.id, { onDelete: "cascade" }),
  fieldId: text("field_id").notNull(),
  value: text("value").notNull().default(""),
});

export const payment = pgTable("payment", {
  id: text("id").primaryKey(),
  receiptNumber: text("receipt_number").notNull().unique(),
  studentId: text("student_id")
    .notNull()
    .references(() => student.id),
  courseId: text("course_id")
    .notNull()
    .references(() => course.id),
  amount: doublePrecision("amount").notNull(),
  paymentDate: text("payment_date").notNull(),
  paymentMode: paymentModeEnum("payment_mode").notNull(),
  status: paymentStatusEnum("status").notNull().default("COMPLETED"),
  transactionId: text("transaction_id"),
  remarks: text("remarks"),
  createdAt: timestamp("created_at").notNull().defaultNow(),
});
