import UpdateSearch from "./update-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

// @ts-expect-error: Ignore implicit 'any' type error for 'searchParams'
export default async function Page({searchParams}) {
    return <>
        <NavigationBar />
        <UpdateSearch searchParams={searchParams} />
    </>
}