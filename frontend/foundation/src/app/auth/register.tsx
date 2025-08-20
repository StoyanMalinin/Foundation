"use client";

import {Box, Button, Input, Paper} from "@mui/material";
import { redirect } from "next/navigation";
import Form from "next/form";
import { useActionState } from "react";

type FormState = {
    username: string
    errorMessage: string,
}

export default function Register() {
    const [formState, formAction] = useActionState(registerAction, {
        username: "",
        errorMessage: "",
    });

    const errorText = (<label style={{color: "red"}}>{formState.errorMessage}</label>)
    return <Box sx={{display: "flex", justifyContent: "center", flexDirection: "column", alignItems: "center"}}>
        <Paper sx={{width: "250px"}}>
            <Form action={formAction}>
                <Box sx={{display: "flex", justifyContent: "center", flexDirection: "column", alignItems: "center"}}>
                    <h2>Register</h2>
                    <Input name="username" placeholder="Username" defaultValue={formState.username} type="text" /><br></br>
                    <Input name="password" placeholder="Password" type="password" /><br></br>
                    <Input name="first_name" placeholder="First name" type="text" /><br></br>
                    <Input name="last_name" placeholder="Last name" type="text" /><br></br>

                    <br></br>
                    <Button type="submit" variant="contained">Register</Button>
                    <br/><br/>

                    {formState.errorMessage != "" && <>{errorText}</>}
                </Box>
            </Form>
        </Paper>
    </Box>
}

async function registerAction(prevState: FormState, formData: FormData): Promise<FormState> {
    let res = await fetch('https://localhost:6969/register', {
        method: "post",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            'username': formData.get("username"),
            'password': formData.get("password"),
            'first_name': formData.get("first_name"),
            'last_name': formData.get("last_name"),
        }),
        credentials: "include",
    });

    if (res.status == 200) {
        redirect('/');
    }

    return {
        username: formData.get("username")?.toString() ?? "",
        errorMessage: await res.text(),
    };
}