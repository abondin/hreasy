/**
 * 401 Error
 */
export class AuthenticationError extends Error {
    constructor(public message: string, public code?: string) {
        super(message);
    }
}

/**
 * 403 Error
 */
export class AccessDeniedError extends Error {
    constructor(public message: string, public code: string) {
        super(message);
    }
}

/**
 * Backend sends 422 error with specific format in case of business error
 * <ul>
 *     <li>code - business error code</li>
 *     <li>attrs - attributes to resolve business error code to user message</li>
 *     <li>message</li> - default message from backend (99% it should be enough)
 * </ul>
 */
export class BusinessError extends Error {
    constructor(
                public message: string,
                public code?: string,
                public attrs?: Map<string, string>) {
        super(message);
    }
}

export class UnknownBackendError extends Error {
    constructor(public message: string) {
        super(message);
    }
}
