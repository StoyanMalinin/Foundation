import AuthWrapper from "@/app/auth/wrapper";
import UpdateSearch from "./update-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page({searchParams}) {
    return <AuthWrapper>
        <NavigationBar />
        <UpdateSearch searchParams={searchParams} />
    </AuthWrapper>
}