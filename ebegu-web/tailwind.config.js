/** @type {import('tailwindcss').Config} */
module.exports = {
    prefix: 'tw-',
    content: [
        './src/**/*.{html,ts}', // adjust if you have multiple apps/libs
        './libs/**/*.{html,ts}'
    ],
    theme: {
        extend: {
            colors: {
                contrast: {
                    default: 'var(--contrast)',
                    light: 'var(--contrast-light)',
                    lightest: 'var(--contrast-lightest)',
                    darkest: 'var(--contrast-darkest)'
                },
                primary: {
                    color: 'var(--primary-color)',
                    'color-dark': 'var(--primary-color-dark)'
                }
            }
        }
    },
    plugins: []
};
