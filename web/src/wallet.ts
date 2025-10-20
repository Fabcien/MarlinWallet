import {Wallet} from 'ecash-wallet';
import {Address} from 'ecash-lib';
import {webViewLog, webViewError} from './common';
import {config} from './config';


// Wallet date that we can't retrieve from ecash-wallet.
// For now it's just the mnemonic.
export interface WalletData {
    mnemonic: string;
}

// Return the eCash address string for this wallet
export function getAddress(wallet: Wallet): string | null {
    if (!wallet || !wallet.address) {
        return null;
    }

    return Address.parse(wallet.address).withPrefix(config.addressPrefix).toString();
}

// Send a transaction
export async function sendTransaction(wallet: Wallet, recipientAddress: string, sats: number) {
    // Create the action with outputs
    const action = wallet.action({
        outputs: [
            {
                address: recipientAddress,
                sats: BigInt(sats)
            }
        ]
    });
    
    const builtTx = action.build();
    await builtTx.broadcast();
}
