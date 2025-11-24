/**
 * Configuration for the Marlin Wallet
 */

export const config = {
    /**
     * Chronik API endpoints
     * Multiple endpoints can be provided for failover
     */
    chronikUrls: [
        'https://chronik-native1.fabien.cash',
        'https://chronik-native2.fabien.cash',
        'https://chronik-native3.fabien.cash',
    ],

    /**
     * Address prefix for the network
     * 'ecash' for mainnet, 'ectest' for testnet
     */
    addressPrefix: 'ecash' as const,

    /**
     * Block explorer base URL
     * Transaction IDs will be appended to this URL
     */
    explorerUrl: 'https://explorer.e.cash/tx/',

    /**
     * Currency ticker symbol
     */
    ticker: 'XEC',

    /**
     * BIP21 prefix
     */
    bip21Prefix: 'ecash:' as const,
};
