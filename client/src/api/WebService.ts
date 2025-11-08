import { UserInfo } from "@/stores/UserInfoStore";
import { CreateListingRequest, CreateListingResponse, DeleteListingRequest, ErrorResponse, GetUserInfoRequest, Listing, RegisterUserRequest, RegisterUserResponse, UpdateListingRequest, ChatRoom, ChatRoomDetailDTO, Message, UpdateUserRequest, UpdateUserResponse, UploadAvatarRequest, UploadAvatarResponse, VerifyUserRequest, VerifyUserResponse, type LoginRequest, type LoginResponse, GetSpecificListingRequest, GetListingsRequest, SaveListingRequest, SaveListingResponse, UnsaveListingRequest, UnsaveListingResponse, CheckSavedStatusRequest, CheckSavedStatusResponse, GetSavedListingsResponse, GetSavedListingIdsResponse } from "./WebTypes";

class AppConfig {
    access_token?: string;
    url: string = "http://localhost:8080";
}
export const appConfig = new AppConfig();

class API {

    constructor() { }

    private async Request<T>(
        path: string,
        method: "GET" | "POST" | "PUT" | "DELETE",
        options: { body?: any; params?: Record<string, string | number> } = {},
        internalMethod: string,
        headerArgs?: Record<string, string>
    ): Promise<T | ErrorResponse> {
        let url = appConfig.url + path;

        if (options.params) {
            for (const [key, value] of Object.entries(options.params)) {
                url = url.replace(`:${key}`, String(value));
            }
        }

        let loop = 0;
        while (loop++ < 2) {

            const headers: Record<string, string> = {
                Accept: "application/json",
                ...headerArgs,
            }

            const isFormData = typeof FormData !== 'undefined' && options.body instanceof FormData;
            if (!isFormData) {
                headers["Content-Type"] = "application/json";
            }

            const notProtectedMethods = ["Login", "Register", "VerifyUser"];
            const isProtectedMethod = !notProtectedMethods.includes(internalMethod);
            if (appConfig.access_token && isProtectedMethod) {
                headers["Authorization"] = `Bearer ${appConfig.access_token}`;
            }

            // console.log(options.body);

            const response = await fetch(url, {
                method,
                headers,
                credentials: 'include',
                body: method !== "GET"
                    ? (isFormData ? (options.body as FormData) : JSON.stringify(options.body))
                    : undefined,
            });

            console.log("Response:", response, '\nMethodName', internalMethod);

            if (response.status === 401 || response.status === 403 && appConfig.access_token) {
                try {
                    await sendRefreshToken();
                    continue;
                } catch (refreshError) {
                    console.error("Refresh token failed:", refreshError);
                    return new ErrorResponse(
                        internalMethod,
                        401,
                        "Unauthorized",
                        { error: "Session expired. Please log in again." }
                    );
                }
            }

            if (!response.ok || (response.status !== 401 && response.status !== 200)) {
                const responseBody = await response.text();
                return new ErrorResponse(internalMethod, response.status, response.statusText, JSON.parse(responseBody));
            }

            if (headerArgs?.["Accept"] === "text/csv") {
                return response.blob() as Promise<T>;
            } else {
                const contentType = response.headers.get('Content-Type');
                if (contentType?.includes('application/json')) {
                    return response.json() as Promise<T>;
                } else {
                    return new ErrorResponse(internalMethod, -1, "Unknown Endpoint", { error: "Unknown content type" });
                }
            }
        }

        return new ErrorResponse(internalMethod, 1111, "Loop Ended", { error: "Unknown error: Loop Ended" });
    }

    async Register(body: RegisterUserRequest): Promise<RegisterUserResponse | ErrorResponse> {
        return this.Request<RegisterUserResponse>("/auth/signup", "POST", { body }, 'Register');
    }

    async Login(body: LoginRequest): Promise<LoginResponse | ErrorResponse> {
        return this.Request<LoginResponse>("/auth/login", "POST", { body }, 'Login');
    }

    async VerifyUser(body: VerifyUserRequest): Promise<VerifyUserResponse | ErrorResponse> {
        return this.Request<VerifyUserResponse>("/auth/verify", "POST", { body }, 'VerifyUser');
    }

    async GetUserInfo(): Promise<UserInfo | ErrorResponse> {
        return this.Request<UserInfo>("/users/me", "GET", {}, 'GetUserInfo');
    }

    //Listing CRUD
    async CreateListing(body: CreateListingRequest): Promise<CreateListingResponse | ErrorResponse> {
        return this.Request<CreateListingResponse>("/listings/create", "POST", { body }, 'CreateListing');
    }

    async GetListings(filters?: GetListingsRequest): Promise<Listing[] | ErrorResponse> {
        // If filters are provided, use POST /listings/filter
        if (filters) {
            return this.Request<Listing[]>("/listings/filter", "POST", { body: filters }, 'GetListings');
        }
        // Otherwise, use GET /listings for all listings
        return this.Request<Listing[]>("/listings", "GET", {}, 'GetListings');
    }

    async GetSpecificListing(request: GetSpecificListingRequest): Promise<Listing | ErrorResponse> {
        return this.Request<Listing>(`/listings/${request.listingGU}`, "GET", {}, 'GetSpecificListing');
    }

    async GetMyListings(): Promise<Listing[] | ErrorResponse> {
        return this.Request<Listing[]>("/listings/me", "GET", {}, 'GetMyListings');
    }

    async UpdateListing(request: UpdateListingRequest): Promise<Listing[] | ErrorResponse> {
        return this.Request<Listing[]>(`/listings/${request.listingGU}`, "PUT", { body: request.body }, 'UpdateListing');
    }

    async DeleteListing(request: DeleteListingRequest): Promise<Listing[] | ErrorResponse> {
        return this.Request<Listing[]>(`/listings/${request.listingGU}`, "DELETE", {}, 'DeleteListing');
    }

    // Messaging
    async GetChatRooms(): Promise<ChatRoom[] | ErrorResponse> {
        return this.Request<ChatRoom[]>("/chat/rooms", "GET", {}, 'GetChatRooms');
    }

    async GetChatRoomDetails(roomId: string): Promise<ChatRoomDetailDTO | ErrorResponse> {
        return this.Request<ChatRoomDetailDTO>(`/chat/rooms/${roomId}`, "GET", {}, 'GetChatRoomDetails');
    }

    async GetRoomMessages(roomId: string, page: number = 0, size: number = 50): Promise<Message[] | ErrorResponse> {
        return this.Request<Message[]>(`/chat/rooms/${roomId}/messages?page=${page}&size=${size}`, "GET", {}, 'GetRoomMessages');
    }

    async MarkMessagesAsRead(roomId: string): Promise<{ result: string } | ErrorResponse> {
        return this.Request<{ result: string }>(`/chat/rooms/${roomId}/mark-read`, "POST", {}, 'MarkMessagesAsRead');
    }

    // User Profile
    async UpdateUserProfile(body: UpdateUserRequest): Promise<UpdateUserResponse | ErrorResponse> {
        return this.Request<UpdateUserResponse>("/users/me/update", "PUT", { body }, 'UpdateUserProfile');
    }

    async UploadAvatar(body: UploadAvatarRequest): Promise<UploadAvatarResponse | ErrorResponse> {
        return this.Request<UploadAvatarResponse>("/uploads/avatar", "POST", { body }, 'UploadAvatar');
    }

    // Saved Listings
    async SaveListing(request: SaveListingRequest): Promise<SaveListingResponse | ErrorResponse> {
        return this.Request<SaveListingResponse>("/saved-listings", "POST", { body: request }, 'SaveListing');
    }

    async UnsaveListing(request: UnsaveListingRequest): Promise<UnsaveListingResponse | ErrorResponse> {
        return this.Request<UnsaveListingResponse>("/saved-listings", "DELETE", { body: request }, 'UnsaveListing');
    }

    async CheckSavedStatus(request: CheckSavedStatusRequest): Promise<CheckSavedStatusResponse | ErrorResponse> {
        return this.Request<CheckSavedStatusResponse>("/saved-listings/status", "POST", { body: request }, 'CheckSavedStatus');
    }

    async GetSavedListings(): Promise<GetSavedListingsResponse | ErrorResponse> {
        return this.Request<GetSavedListingsResponse>("/saved-listings", "GET", {}, 'GetSavedListings');
    }

    async GetSavedListingIds(): Promise<GetSavedListingIdsResponse | ErrorResponse> {
        return this.Request<GetSavedListingIdsResponse>("/saved-listings/ids", "GET", {}, 'GetSavedListingIds');
    }
}

async function sendRefreshToken() {
    const headers = {
        "Content-Type": "application/json",
        // "Authorization": `Bearer ${appConfig.refresh_token}`,
    }

    appConfig.access_token = undefined;

    const response = await fetch(`${appConfig.url}/auth/refresh`, {
        body: '{}',
        headers,
        credentials: 'include',
        method: "POST",
    });
    const contentType = response.headers.get('Content-Type');
    if (response.ok && contentType?.includes('application/json')) {
        const data = await response.json() as { accessToken: string; refreshToken: string };
        appConfig.access_token = data.accessToken;
        SaveAppConfig();
    } else {
        throw new Error('Bad refresh token response');
    }
}

export function SaveAppConfig() {
    let config = JSON.stringify(appConfig);
    localStorage.setItem("AppConfig", config);
}

export function LoadAppConfig() {
    let config = localStorage.getItem("AppConfig");
    if (config) {
        let obj = JSON.parse(config) as AppConfig;
        appConfig.access_token = obj.access_token;
        console.log("AppConfig:", appConfig);
    } else {
        console.log("No AppConfig found");
    }
}

export default new API();