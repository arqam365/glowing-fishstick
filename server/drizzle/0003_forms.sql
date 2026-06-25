CREATE TYPE "public"."field_type" AS ENUM('TEXT', 'PARAGRAPH', 'NUMBER', 'DATE', 'DROPDOWN', 'CHECKBOX', 'RADIO');--> statement-breakpoint
CREATE TABLE "form_template" (
	"id" text PRIMARY KEY NOT NULL,
	"title" text NOT NULL,
	"description" text DEFAULT '' NOT NULL,
	"created_at" timestamp DEFAULT now() NOT NULL,
	"updated_at" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "form_field" (
	"id" text PRIMARY KEY NOT NULL,
	"form_id" text NOT NULL,
	"label" text NOT NULL,
	"type" "field_type" NOT NULL,
	"required" boolean DEFAULT false NOT NULL,
	"options" text DEFAULT '[]' NOT NULL,
	"order" integer DEFAULT 0 NOT NULL
);
--> statement-breakpoint
CREATE TABLE "form_response" (
	"id" text PRIMARY KEY NOT NULL,
	"form_id" text NOT NULL,
	"submitted_at" text NOT NULL,
	"created_at" timestamp DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "form_answer" (
	"id" text PRIMARY KEY NOT NULL,
	"response_id" text NOT NULL,
	"field_id" text NOT NULL,
	"value" text DEFAULT '' NOT NULL
);
--> statement-breakpoint
ALTER TABLE "form_field" ADD CONSTRAINT "form_field_form_id_form_template_id_fk" FOREIGN KEY ("form_id") REFERENCES "public"."form_template"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "form_response" ADD CONSTRAINT "form_response_form_id_form_template_id_fk" FOREIGN KEY ("form_id") REFERENCES "public"."form_template"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "form_answer" ADD CONSTRAINT "form_answer_response_id_form_response_id_fk" FOREIGN KEY ("response_id") REFERENCES "public"."form_response"("id") ON DELETE cascade ON UPDATE no action;
