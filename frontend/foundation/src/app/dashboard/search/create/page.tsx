export const dynamic = "force-dynamic";

import CreateSearch from "./create-search";
import { NavigationBar } from "@/app/navigation/navigation-bar";

export default async function Page() {
    return (
        <>
            <NavigationBar />
            <CreateSearch />
        </>
    );
}
