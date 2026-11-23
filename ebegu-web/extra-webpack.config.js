const webpack = require('webpack');

module.exports = {
    resolve: {
        fallback: {
            process: require.resolve('process/browser')
        }
    },
    plugins: [
        new webpack.ProvidePlugin({
            process: 'process/browser'
        })
    ],
    module: {
        rules: [
            {
                test: /\.html$/,
                // Angular component templates (templateUrl) are loaded with ?ngResource in JIT/test builds and
                // must stay raw. html-loader is only meant for the AngularJS templates.
                resourceQuery: {not: [/ngResource/]},
                loader: 'html-loader',
                options: {
                    attributes: false,
                    esModule: false
                }
            }
        ]
    }
};
