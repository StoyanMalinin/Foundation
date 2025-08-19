"use client";

import {SearchesMetadata} from "@/app/dashboard/search/data";
import {InputLabel, Switch} from "@mui/material";
import {useState} from "react";
import SearchesMetadataListClient from "@/app/dashboard/search/searches-metadata-list-client";

export default function SearchesMetadataListWithSwitch({searches, adminSearches}: {
    searches: SearchesMetadata[],
    adminSearches: SearchesMetadata[],
}) {
    const [checked, setChecked] = useState(false);
    const shownSearches = checked ? adminSearches : searches;

    return <>
        <InputLabel htmlFor="admin-switch">Admin only</InputLabel>
        <Switch id="admin-switch" checked={checked} onClick={() => setChecked(!checked)} />
        <SearchesMetadataListClient searches={shownSearches} adminSearches={adminSearches} />
    </>
}