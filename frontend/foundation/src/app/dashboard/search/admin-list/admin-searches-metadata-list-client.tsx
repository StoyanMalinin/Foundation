"use client";

import { Table, TableBody, TableCell, TableHead, TableRow } from "@mui/material";

type SearchesMetadata = {
    title: string;
}

export default function AdminSearchesMetadataListClient({searches}: {searches: SearchesMetadata[]}) {
    return <Table>
        <TableHead>
            <TableRow>
                <TableCell align="center" sx={{borderBottom: '1px solid black'}}><b>Title</b></TableCell>
                <TableCell align="center" sx={{borderBottom: '1px solid black'}}><b>Actions</b></TableCell>
            </TableRow>
        </TableHead>
        <TableBody>
            {searches.map((item: { title: string; }) => (
                <TableRow key={item.title}>
                    <TableCell align="center">{item.title}</TableCell>
                    <TableCell align="center">TODO</TableCell>
                </TableRow>
            ))}
        </TableBody>
    </Table>
}