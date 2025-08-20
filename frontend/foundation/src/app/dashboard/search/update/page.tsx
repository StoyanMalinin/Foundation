import UpdateSearch from "./update-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page({searchParams}) {
    return <>
        <NavigationBar />
        <UpdateSearch searchParams={searchParams} />
    </>
}