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

  constructor(code: number, message: string) {
    super(message);
    this.code = code;
    this.message = message;
    this.name = "ApiError";
  }
}
