"use client";

import { Box, Paper } from "@mui/material";
import { Search } from "../data";

export default function ViewSearchClient({search}: {search: Search}) {
    console.log("sssssss", search);
    return (
        <Box sx={{display: "flex", justifyContent: "center"}}>
            <Paper sx={{width: "500px", margin: "5px"}}>
                <h1>{search.title}</h1>
                <h2>Created on {new Date(search.created_at).toDateString()}</h2>
                <p>{search.description}</p>
            </Paper>
        </Box>
    );
}
