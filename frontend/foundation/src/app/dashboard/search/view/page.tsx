import ViewSearchServer from "./view-search-server";

export default async function Page({searchParams}) {
    const id = (await searchParams).id;

    return (
        <ViewSearchServer searchId={parseInt(id ?? "0")} />
    );
}