/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        './src/**/*.{html,ts}', // adjust if you have multiple apps/libs
        './libs/**/*.{html,ts}'
    ],
    theme: {
        extend: {}
    },
    plugins: []
};
