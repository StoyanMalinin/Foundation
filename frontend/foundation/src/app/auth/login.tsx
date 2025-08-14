"use client";

import { Button, Input } from "@mui/material";
import { redirect } from "next/navigation";
import Form from "next/form";
import { useActionState } from "react";

type FormState = {
    username: string
    errorMessage: string,
}

export default function Login() {
    const [formState, formAction] = useActionState(loginAction, {
        username: "",
        errorMessage: ""
    });

    const errorText = (<label style={{color: "red"}}>{formState.errorMessage}</label>)
    return <Form action={formAction}>
        <h2>Login</h2>
        <Input name="username" placeholder="Username" defaultValue={formState.username} type="text" /><br></br>
        <Input name="password" placeholder="Password" type="password" /><br></br>
        
        <br></br>
        <Button type="submit" variant="contained">Login</Button>

        {formState.errorMessage != "" && <><br/><br/>{errorText}</>}
    </Form>
}

async function loginAction(prevState: FormState, formData: FormData): Promise<FormState> {
    let res = await fetch('https://localhost:6969/login', {
        method: "post",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({
            'username': formData.get("username"),
            'password': formData.get("password"),
        }),
        credentials: "include",
    });

    if (res.status == 200) {
        redirect('/');
    }

    return {
        username: formData.get("username")?.toString() ?? "",
        errorMessage: await res.text()
    };
}