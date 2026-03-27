export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export interface ApiError {
  code: number;
  message: string;
}

export class ApiErrorClass extends Error {
  public readonly code: number;
  public readonly message: string;
  public readonly details?: unknown;

  constructor(code: number, message: string, details?: unknown) {
    super(message);
    this.code = code;
    this.message = message;
    this.details = details;
    this.name = "ApiError";
  }
}
