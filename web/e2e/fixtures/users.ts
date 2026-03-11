/**
 * Test user role aliases used by E2E helpers.
 * Credentials are read from environment variables in auth fixture.
 */
export const testRoles = [
  "employee",
  "mentor_author",
  "mentor_non_author",
  "admin_employees",
  "admin_juniors",
  "overtime_admin",
  "readonly",
] as const;

export type TestRole = (typeof testRoles)[number];

export interface TestCredentials {
  username: string;
  password: string;
}
