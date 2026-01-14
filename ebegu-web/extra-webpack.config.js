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
                loader: 'html-loader',
                options: {
                    attributes: false,
                    esModule: false
                }
            }
        ]
    }
};
