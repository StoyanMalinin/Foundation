import { NavigationBar } from "@/app/navigation/navigation-bar";
import ViewSearchServer from "./view-search-server";

// @ts-expect-error: Ignore implicit 'any' type error for 'searchParams'
export default async function Page({searchParams}) {
    const id = (await searchParams).id;

    return (
        <>
            <NavigationBar />
            <ViewSearchServer searchId={parseInt(id ?? "0")} />
        </>
    );
}