import { NavigationBar } from "@/app/navigation/navigation-bar";
import ViewSearchServer from "./view-search-server";
import AuthWrapper from "@/app/auth/wrapper";

export default async function Page({searchParams}) {
    const id = (await searchParams).id;

    return (
        <AuthWrapper>
            <NavigationBar />
            <ViewSearchServer searchId={parseInt(id ?? "0")} />
        </AuthWrapper>
    );
}