import { ErrorResponse, RegisterUserRequest, RegisterUserResponse, type LoginRequest, type LoginResponse } from "./WebTypes";

class API {
    API_URL: string = "http://localhost:8080";

    constructor() { }

    private async Request<T>(
        path: string,
        method: "GET" | "POST" | "PUT" | "DELETE",
        options: { body?: unknown; params?: Record<string, string | number> } = {},
        internalMethod: string,
        headerArgs?: Record<string, string>
    ): Promise<T | ErrorResponse> {
        let url = this.API_URL + path;

        if (options.params) {
            for (const [key, value] of Object.entries(options.params)) {
                url = url.replace(`:${key}`, String(value));
            }
        }

        const headers: Record<string, string> = {
            Accept: "application/json",
            ...headerArgs,
        }

        const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
        if (!isFormData) {
            headers["Content-Type"] = "application/json";
        }

        console.log(options.body);

        const response = await fetch(url, {
            method,
            headers,
            credentials: 'include',
            body: method !== "GET"
                ? (isFormData ? (options.body as FormData) : JSON.stringify(options.body))
                : undefined,
        });

        if (!response.ok) {
            const responseBody = await response.text();
            return new ErrorResponse(internalMethod, response.status, response.statusText, responseBody);
        }

        if (headerArgs?.["Accept"] === "text/csv") {
            return response.blob() as Promise<T>;
        } else {
            let text = await response.text();
            console.log("Cookie Body" + text);
            if (!text){
                return response as unknown as Promise<T>;
            }
            return response.json() as Promise<T>;
        }
    }

    async Register(body: RegisterUserRequest): Promise<RegisterUserResponse | ErrorResponse> {
        return this.Request<RegisterUserResponse>("/auth/signup", "POST", { body }, 'Register');
    }

    async Login(body: LoginRequest): Promise<LoginResponse | ErrorResponse> {
        return this.Request<LoginResponse>("/auth/login", "POST", { body }, 'Login');
    }
}

export default new API();