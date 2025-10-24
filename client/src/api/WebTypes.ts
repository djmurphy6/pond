export class ErrorResponse {
    name?: string;
    code?: number;
    statusText?: string;
    body?: { error: string };

    constructor(name: string, code: number, statusText: string, body?: { error: string }) {
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
    accessToken: string;
}

//GET USER INFO
export type GetUserInfoRequest = {
    // email: string;
}

//CREATE LISTING
export type CreateListingRequest = {
    description: string;
    picture1_url?: string;
    picture2_url?: string;
    price: number;
    condition: string;
    title: string;
}
export type CreateListingResponse = Listing

//GET LISTING
export type Listing = {
    usergu: string;
    listinggu: string;
    description: string;
    picture1_url: string;
    picture2_url: string;
    price: number;
    condition: string;
    title: string;
}
