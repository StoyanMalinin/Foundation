"use client";

import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Button from '@mui/material/Button';
import Box from '@mui/material/Box';
import Link from 'next/link';
import { redirect } from 'next/navigation';

export function NavigationBar() {
  return (
    <AppBar position="static" color="default" elevation={1} sx={{height: "40px"}}>
        <Toolbar sx={{height: "100%"}}>
          <Box sx={{ flexGrow: 1, height: "90%" }}>
            <Link href="/dashboard/search">
              <Button variant="contained" size="small">
                Dashboard
              </Button>
            </Link>
          </Box>
          <Box sx={{ height: "90%" }}>
            <Button
              variant="contained"
              size="small"
              color="error"
              onClick={onLogout}
            >
              Logout
            </Button>
          </Box>
      </Toolbar>
    </AppBar>
  );
};

async function onLogout() {
  await fetch ('https://localhost:6969/logout', {
    method: 'POST',
    credentials: 'include',
  });
  redirect('/');
}
