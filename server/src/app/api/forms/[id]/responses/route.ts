import { db } from "@/lib/db";
import { formTemplate, formResponse, formAnswer } from "@/db/schema";
import { requireAuth } from "@/lib/auth-guard";
import { NextRequest, NextResponse } from "next/server";
import { nanoid } from "nanoid";
import { eq, desc } from "drizzle-orm";

export async function GET(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { id } = await params;
  const responses = await db.select().from(formResponse)
    .where(eq(formResponse.formId, id))
    .orderBy(desc(formResponse.createdAt));

  const answers = await db.select().from(formAnswer);

  const result = responses.map((r) => ({
    ...r,
    answers: answers.filter((a) => a.responseId === r.id),
  }));

  return NextResponse.json(result);
}

export async function POST(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { error } = await requireAuth(req);
  if (error) return error;

  const { id } = await params;

  const formExists = await db.select().from(formTemplate).where(eq(formTemplate.id, id));
  if (formExists.length === 0) return NextResponse.json({ error: "Form not found" }, { status: 404 });

  const body = await req.json();
  const { answers = [], submittedAt } = body;

  const today = submittedAt ?? new Date().toISOString().split("T")[0];
  const responseId = nanoid();

  const [response] = await db.insert(formResponse).values({
    id: responseId,
    formId: id,
    submittedAt: today,
    createdAt: new Date(),
  }).returning();

  const insertedAnswers = answers.length > 0
    ? await db.insert(formAnswer).values(
        answers.map((a: { fieldId: string; value: string }) => ({
          id: nanoid(),
          responseId,
          fieldId: a.fieldId,
          value: a.value ?? "",
        }))
      ).returning()
    : [];

  return NextResponse.json({ ...response, answers: insertedAnswers }, { status: 201 });
}
