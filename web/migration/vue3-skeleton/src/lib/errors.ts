export class AuthenticationError extends Error {
  constructor(
    message: string,
    public code?: string,
  ) {
    super(message);
    this.name = "AuthenticationError";
  }
}

export class AccessDeniedError extends Error {
  constructor(
    message: string,
    public code?: string,
  ) {
    super(message);
    this.name = "AccessDeniedError";
  }
}

export class BusinessError extends Error {
  constructor(
    message: string,
    public code?: string,
    public attrs?: Record<string, string>,
  ) {
    super(message);
    this.name = "BusinessError";
  }
}

export class UnknownBackendError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "UnknownBackendError";
  }
}

export const errorUtils = {
  shortMessage(error: unknown): string {
    if (!error || typeof error === "undefined") {
      return "Unknown Error without any description";
    }
    if (error instanceof AccessDeniedError) {
      return error.message;
    }
    if (error instanceof AuthenticationError) {
      return error.message;
    }
    if (error instanceof BusinessError) {
      return error.message;
    }
    if (error instanceof UnknownBackendError) {
      return error.message;
    }
    if (error instanceof Error) {
      return error.message;
    }
    return String(error);
  },

  isAccessDenied(error: unknown): boolean {
    return error instanceof AccessDeniedError;
  },
};
