import Form from 'next/form'

const fetch = require('node-fetch');
import { redirect } from 'next/navigation';

export default async function CreateSearchForm() {
    return (<>
        <Form action={createSearchAPI}>
            <input type="text" name="name" placeholder="" required />
            <input type="text" name="description" placeholder="" required />
        </Form>
    </>);
}

async function createSearchAPI(formData: FormData) {
    "use server";
    
    const name = formData.get("name") as string;
    const description = formData.get("description") as string;

    const response = await fetch('localhost:6969/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify({ name, description }),
    });
}
