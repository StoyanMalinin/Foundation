import AuthWrapper from "@/app/auth/wrapper";
import UpdateSearch from "./update-search";

export default async function Page({searchParams}) {
    return <AuthWrapper>
        <UpdateSearch searchParams={searchParams} />
    </AuthWrapper>
}