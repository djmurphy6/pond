export class ErrorResponse {
    name?: string;
    code?: number;
    statusText?: string;
    body?: string;

    constructor(name: string, code: number, statusText: string, body?: string) {
        this.name = name;
        this.code = code;
        this.statusText = statusText;
        this.body = body;
    }
}

// REGISTER
export type RegisterUserRequest = {
    email: string
    password: string
    username: string
}

export type RegisterUserResponse = {
    //Empty
}

//LOGIN
export type LoginRequest = {
    email: string;
    password: string;
}
export type LoginResponse = {
    token: string;
    expiresIn: number;
}
