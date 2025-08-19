"use client";

import React, { useEffect, useState } from 'react';
import Link from 'next/link';
import { Box, Typography, Button, Stack, Paper } from '@mui/material';


export function DashboardSearch() {
    const [name, setName] = useState<{firstName: string, lastName: string} | null>(null);
    useEffect(() => {
        const fetchData = async () => {
            const response = await fetch("https://localhost:6969/who-am-i", {
                method: "GET",
                credentials: "include",
            });
            const data = await response.json();
            setName({ firstName: data.first_name, lastName: data.last_name });
        };
        
        fetchData();
    }, []);

    if (name == null) {
        return <p>Loading...</p>;
    }

    return (
    <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center">
        <Typography variant="h4" component="h1" gutterBottom>
            Hi, {name.firstName} {name.lastName}
        </Typography>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={4} justifyContent="center" mt={4}>
            <Link href="/dashboard/search/list" passHref legacyBehavior>
            <Button variant="contained" size="large" sx={{ minWidth: 180, minHeight: 80, fontSize: 24, borderRadius: 3 }}>
                List Searches
            </Button>
            </Link>
            <Link href="/dashboard/search/create" passHref legacyBehavior>
            <Button variant="contained" size="large" sx={{ minWidth: 180, minHeight: 80, fontSize: 24, borderRadius: 3 }}>
                Create Search
            </Button>
            </Link>
        </Stack>
    </Box>
  );
};

export default DashboardSearch;
