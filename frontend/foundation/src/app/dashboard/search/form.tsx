import { Button, Input } from "@mui/material";
import Form from "next/form";

export default function SearchForm(formAction: (formData: FormData) => Promise<void>) {
    return (
        <Form action={formAction}>
            <Input type="text" placeholder="Title" name="title" /><br></br>
            <Input type="text" placeholder="Description" name="description" /><br></br>

            <Button type="submit">Search</Button>
        </Form>
    );
}
