/**
 * Configuration for the eCash Wallet
 */

export const config = {
    /**
     * Chronik API endpoints
     * Multiple endpoints can be provided for failover
     */
    chronikUrls: ['https://chronik-testnet2.fabien.cash'],

    /**
     * Address prefix for the network
     * 'ecash' for mainnet, 'ectest' for testnet
     */
    addressPrefix: 'ectest' as const,

    /**
     * Block explorer base URL
     * Transaction IDs will be appended to this URL
     */
    explorerUrl: 'https://texplorer.e.cash/tx/',
};
