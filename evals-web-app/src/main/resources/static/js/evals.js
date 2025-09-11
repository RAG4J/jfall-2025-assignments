// JavaScript utilities for Evals Manager
console.log('Evals Manager loaded');

// Include SockJS and STOMP for WebSocket connectivity
// These should be loaded before this script in the HTML template

// Progress tracking and WebSocket utilities
window.ProgressTracker = {
    stompClient: null,
    subscriptions: new Map(),
    
    // Initialize WebSocket connection
    connect: function(callback) {
        if (this.stompClient && this.stompClient.connected) {
            if (callback) callback();
            return;
        }
        
        const socket = new SockJS('/ws');
        this.stompClient = Stomp.over(socket);
        
        // Disable debug logging
        this.stompClient.debug = null;
        
        const self = this;
        this.stompClient.connect({}, function(frame) {
            console.log('Connected to WebSocket: ' + frame);
            if (callback) callback();
        }, function(error) {
            console.error('WebSocket connection error: ', error);
            // Attempt to reconnect after 5 seconds
            setTimeout(() => self.connect(callback), 5000);
        });
    },
    
    // Subscribe to progress updates for a specific run
    subscribeToRunProgress: function(runId, callback) {
        if (!this.stompClient || !this.stompClient.connected) {
            console.error('WebSocket not connected. Cannot subscribe to run progress.');
            return null;
        }
        
        const topic = '/topic/progress/' + runId;
        const subscription = this.stompClient.subscribe(topic, function(message) {
            const progress = JSON.parse(message.body);
            callback(progress);
        });
        
        this.subscriptions.set(runId, subscription);
        console.log('Subscribed to progress updates for run: ' + runId);
        return subscription;
    },
    
    // Subscribe to general progress updates
    subscribeToAllProgress: function(callback) {
        if (!this.stompClient || !this.stompClient.connected) {
            console.error('WebSocket not connected. Cannot subscribe to all progress.');
            return null;
        }
        
        const subscription = this.stompClient.subscribe('/topic/progress', function(message) {
            const progress = JSON.parse(message.body);
            callback(progress);
        });
        
        console.log('Subscribed to all progress updates');
        return subscription;
    },
    
    // Unsubscribe from a run's progress updates
    unsubscribeFromRunProgress: function(runId) {
        const subscription = this.subscriptions.get(runId);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(runId);
            console.log('Unsubscribed from progress updates for run: ' + runId);
        }
    },
    
    // Disconnect WebSocket
    disconnect: function() {
        if (this.stompClient && this.stompClient.connected) {
            // Unsubscribe from all subscriptions
            this.subscriptions.forEach((subscription, runId) => {
                subscription.unsubscribe();
            });
            this.subscriptions.clear();
            
            this.stompClient.disconnect(() => {
                console.log('Disconnected from WebSocket');
            });
        }
    },
    
    // Check if WebSocket is connected
    isConnected: function() {
        return this.stompClient && this.stompClient.connected;
    }
};

// Progress Bar UI Manager
window.ProgressUI = {
    currentRunId: null,
    modal: null,
    progressBar: null,
    progressText: null,
    statusText: null,
    estimatedTime: null,
    
    // Initialize progress UI elements
    init: function() {
        this.modal = document.getElementById('progressModal');
        this.progressBar = document.getElementById('progressBar');
        this.progressText = document.getElementById('progressText');
        this.statusText = document.getElementById('statusText');
        this.estimatedTime = document.getElementById('estimatedTime');
        
        // Create modal if it doesn't exist
        if (!this.modal) {
            this.createProgressModal();
        }
        
        return this;
    },
    
    // Create the progress modal dynamically
    createProgressModal: function() {
        const modalHTML = `
            <div class="modal fade" id="progressModal" tabindex="-1" aria-labelledby="progressModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="progressModalLabel">Execution Progress</h5>
                        </div>
                        <div class="modal-body">
                            <div class="mb-3">
                                <div class="progress">
                                    <div class="progress-bar progress-bar-striped progress-bar-animated" 
                                         id="progressBar" role="progressbar" 
                                         style="width: 0%" 
                                         aria-valuenow="0" 
                                         aria-valuemin="0" 
                                         aria-valuemax="100"></div>
                                </div>
                            </div>
                            <div class="text-center mb-2">
                                <span id="progressText" class="fw-bold">0%</span>
                            </div>
                            <div class="text-center mb-2">
                                <span id="statusText" class="text-muted">Initializing...</span>
                            </div>
                            <div class="text-center">
                                <small id="estimatedTime" class="text-muted">Calculating time remaining...</small>
                            </div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" 
                                    id="progressCloseBtn">Close</button>
                        </div>
                    </div>
                </div>
            </div>`;
        
        document.body.insertAdjacentHTML('beforeend', modalHTML);
        
        // Re-initialize elements
        this.modal = document.getElementById('progressModal');
        this.progressBar = document.getElementById('progressBar');
        this.progressText = document.getElementById('progressText');
        this.statusText = document.getElementById('statusText');
        this.estimatedTime = document.getElementById('estimatedTime');
    },
    
    // Show progress modal for a run
    showProgress: function(runId) {
        this.currentRunId = runId;
        
        // Initialize progress display
        this.updateProgress({
            runId: runId,
            percentage: 0,
            currentStep: 0,
            totalSteps: 0,
            message: 'Initializing execution...',
            status: 'RUNNING',
            estimatedTimeRemainingSeconds: null
        });
        
        // Show modal
        if (this.modal) {
            const bsModal = new bootstrap.Modal(this.modal);
            bsModal.show();
            
            // Subscribe to progress updates
            ProgressTracker.connect(() => {
                ProgressTracker.subscribeToRunProgress(runId, (progress) => {
                    this.updateProgress(progress);
                });
            });
        }
    },
    
    // Update progress display
    updateProgress: function(progress) {
        if (!progress || progress.runId !== this.currentRunId) {
            return;
        }
        
        // Check if this is a token error
        if (this.isTokenError(progress)) {
            this.showTokenError(progress);
            return;
        }
        
        // Update progress bar
        if (this.progressBar) {
            const percentage = Math.round(progress.percentage || 0);
            this.progressBar.style.width = percentage + '%';
            this.progressBar.setAttribute('aria-valuenow', percentage);
            
            // Update progress bar color based on status
            this.progressBar.className = 'progress-bar progress-bar-striped progress-bar-animated';
            if (progress.status === 'COMPLETED') {
                this.progressBar.classList.add('bg-success');
            } else if (progress.status === 'FAILED') {
                this.progressBar.classList.add('bg-danger');
            } else {
                this.progressBar.classList.add('bg-primary');
            }
        }
        
        // Update progress text
        if (this.progressText) {
            const percentage = Math.round(progress.percentage || 0);
            const stepText = progress.totalSteps > 0 ? 
                ` (${progress.currentStep}/${progress.totalSteps})` : '';
            this.progressText.textContent = percentage + '%' + stepText;
        }
        
        // Update status message
        if (this.statusText) {
            this.statusText.textContent = progress.message || 'Processing...';
        }
        
        // Update estimated time
        if (this.estimatedTime) {
            if (progress.estimatedTimeRemainingSeconds !== null && progress.estimatedTimeRemainingSeconds !== undefined) {
                const minutes = Math.floor(progress.estimatedTimeRemainingSeconds / 60);
                const seconds = Math.floor(progress.estimatedTimeRemainingSeconds % 60);
                if (minutes > 0) {
                    this.estimatedTime.textContent = `Estimated time remaining: ${minutes}m ${seconds}s`;
                } else {
                    this.estimatedTime.textContent = `Estimated time remaining: ${seconds}s`;
                }
            } else if (progress.status === 'COMPLETED') {
                this.estimatedTime.textContent = 'Execution completed! Page will refresh shortly...';
            } else if (progress.status === 'FAILED') {
                if (this.isTokenError(progress)) {
                    this.estimatedTime.textContent = 'Execution failed due to token expiration.';
                } else {
                    this.estimatedTime.textContent = 'Execution failed. Page will refresh shortly...';
                }
            } else {
                this.estimatedTime.textContent = 'Calculating time remaining...';
            }
        }
        
        // Handle completion and failures
        if (progress.status === 'COMPLETED') {
            // Update progress bar to show completion with success color
            if (this.progressBar) {
                this.progressBar.className = 'progress-bar bg-success';
            }
            
            setTimeout(() => {
                this.hideProgress();
                // Reload the page to show updated run status
                console.log('Run completed, reloading page to show updated status');
                window.location.reload();
            }, 2500); // 2.5 seconds to let user see completion message
        } else if (progress.status === 'FAILED' && !this.isTokenError(progress)) {
            // Non-token errors auto-hide after 4 seconds and reload to show failed status
            setTimeout(() => {
                this.hideProgress();
                console.log('Run failed, reloading page to show updated status');
                window.location.reload();
            }, 4000);
        }
        // Token errors stay open until manually closed (no reload needed)
    },
    
    // Hide progress modal
    hideProgress: function() {
        if (this.modal) {
            const bsModal = bootstrap.Modal.getInstance(this.modal);
            if (bsModal) {
                bsModal.hide();
            }
        }
        
        // Unsubscribe from progress updates
        if (this.currentRunId) {
            ProgressTracker.unsubscribeFromRunProgress(this.currentRunId);
            this.currentRunId = null;
        }
    },
    
    // Get current progress from server (fallback)
    fetchProgress: function(runId, callback) {
        fetch(`/api/progress/${runId}`)
            .then(response => {
                if (response.ok) {
                    return response.json();
                }
                throw new Error('Progress not found');
            })
            .then(progress => {
                if (callback) callback(progress);
            })
            .catch(error => {
                console.error('Failed to fetch progress:', error);
            });
    },
    
    // Check if progress indicates a token error
    isTokenError: function(progress) {
        if (!progress || !progress.message) return false;
        
        const message = progress.message.toLowerCase();
        return progress.status === 'FAILED' && (
            message.includes('token') && (message.includes('expired') || message.includes('invalid')) ||
            message.includes('401') ||
            message.includes('unauthorized') ||
            message.includes('🔐')
        );
    },
    
    // Show token error in the progress modal
    showTokenError: function(progress) {
        // Update progress bar to show error state
        if (this.progressBar) {
            this.progressBar.className = 'progress-bar bg-warning';
            this.progressBar.style.width = '100%';
        }
        
        // Update progress text
        if (this.progressText) {
            this.progressText.textContent = 'Authentication Required';
        }
        
        // Update status with token error message
        if (this.statusText) {
            this.statusText.innerHTML = `
                <div class="text-warning mb-2">
                    <i class="fas fa-key me-2"></i>
                    <strong>Token Expired</strong>
                </div>
                <div class="small">
                    Your authentication token has expired. Please refresh it to continue.
                </div>
            `;
        }
        
        // Update estimated time with action buttons
        if (this.estimatedTime) {
            this.estimatedTime.innerHTML = `
                <div class="mt-3">
                    <a href="http://localhost:8080" target="_blank" class="btn btn-warning btn-sm me-2">
                        <i class="fas fa-external-link-alt me-1"></i>
                        Open Token Management
                    </a>
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="ProgressUI.hideProgress()">
                        <i class="fas fa-times me-1"></i>
                        Close
                    </button>
                </div>
            `;
        }
        
        // Update modal title to indicate token error
        const modalTitle = document.getElementById('progressModalLabel');
        if (modalTitle) {
            modalTitle.innerHTML = '<i class="fas fa-key me-2"></i>Authentication Required';
        }
        
        // Add token error styling to modal
        if (this.modal) {
            const modalContent = this.modal.querySelector('.modal-content');
            if (modalContent) {
                modalContent.classList.add('border-warning');
            }
            
            const modalHeader = this.modal.querySelector('.modal-header');
            if (modalHeader) {
                modalHeader.className = 'modal-header bg-warning text-dark';
            }
        }
    }
};

// Common utilities that could be shared across pages
window.EvalUtils = {
    // Show loading state on buttons
    setButtonLoading: function(button, loading = true) {
        if (loading) {
            button.disabled = true;
            const originalText = button.innerHTML;
            button.setAttribute('data-original-text', originalText);
            button.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status"></span>Loading...';
        } else {
            button.disabled = false;
            const originalText = button.getAttribute('data-original-text');
            if (originalText) {
                button.innerHTML = originalText;
                button.removeAttribute('data-original-text');
            }
        }
    },

    // Format timestamps consistently
    formatTimestamp: function(timestamp) {
        return new Date(timestamp).toLocaleString();
    },

    // Confirm dialog utility
    confirm: function(message, callback) {
        if (confirm(message)) {
            callback();
        }
    }
};

// Auto-hide alerts after 5 seconds
document.addEventListener('DOMContentLoaded', function() {
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(function(alert) {
        setTimeout(function() {
            const bsAlert = new bootstrap.Alert(alert);
            bsAlert.close();
        }, 5000);
    });
    
    // Initialize progress UI
    ProgressUI.init();
    
    // Connect to WebSocket for real-time updates
    ProgressTracker.connect();
});

// Cleanup on page unload
window.addEventListener('beforeunload', function() {
    ProgressTracker.disconnect();
});

// Helper function to execute a run with progress tracking
function executeRunWithProgress(runId, executeUrl) {
    console.log('Executing run with progress tracking:', { runId, executeUrl });
    
    // Show progress modal
    ProgressUI.showProgress(runId);
    
    // Execute the run
    fetch(executeUrl, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        console.log('Received response:', { 
            status: response.status, 
            statusText: response.statusText,
            ok: response.ok,
            headers: Object.fromEntries(response.headers.entries())
        });
        
        if (response.status === 401) {
            // Handle token expiration
            throw new TokenError('Authentication token has expired');
        }
        if (!response.ok) {
            // Try to parse as JSON first, then fall back to text
            return response.text().then(responseText => {
                console.log('Error response text:', responseText);
                
                try {
                    const errorData = JSON.parse(responseText);
                    if (errorData.type === 'TOKEN_EXPIRED') {
                        throw new TokenError(errorData.message, errorData.instructions);
                    }
                    throw new Error(errorData.message || errorData.error || 'Failed to execute run');
                } catch (jsonError) {
                    // Response is not JSON, likely HTML error page
                    if (responseText.includes('pattern')) {
                        throw new Error('Server response format error. The endpoint may not be returning the expected JSON format.');
                    }
                    throw new Error(`Server error: ${response.status} ${response.statusText}`);
                }
            });
        }
        return response.json();
    })
    .then(data => {
        console.log('Run execution started successfully:', data);
        
        // Verify the response structure
        if (!data.success) {
            throw new Error(data.message || 'Run execution was not successful');
        }
        
        // Progress updates will be received via WebSocket
    })
    .catch(error => {
        console.error('Error executing run:', error);
        
        if (error instanceof TokenError) {
            // Handle token errors specifically
            showTokenErrorAlert(error.message, error.instructions);
        } else {
            ProgressUI.hideProgress();
            
            // Provide more helpful error messages
            let errorMessage = error.message;
            if (errorMessage.includes('pattern')) {
                errorMessage = 'Server configuration error. Please check that the API endpoint is properly configured.';
            }
            
            alert('Failed to execute run: ' + errorMessage);
        }
    });
}

// Custom error class for token errors
class TokenError extends Error {
    constructor(message, instructions) {
        super(message);
        this.name = 'TokenError';
        this.instructions = instructions;
    }
}

// Show token error alert at the top of the page
function showTokenErrorAlert(message, instructions) {
    // Remove any existing token error alerts
    const existingAlerts = document.querySelectorAll('.token-error-alert');
    existingAlerts.forEach(alert => alert.remove());
    
    // Create token error alert
    const alertHTML = `
        <div class="alert alert-warning alert-dismissible fade show token-error-alert" role="alert">
            <h5 class="alert-heading">
                <i class="fas fa-key me-2"></i>
                Authentication Token Required
            </h5>
            <p class="mb-3">${message || 'Your authentication token has expired.'}</p>
            
            <div class="mb-3">
                <h6>To refresh your token:</h6>
                <ol class="mb-0">
                    <li>Open the <strong>main Thymeleaf Agent application</strong> (usually at <code>http://localhost:8080</code>)</li>
                    <li>Navigate to the <strong>Token Management</strong> page</li>
                    <li>Generate a new token or refresh your existing token</li>
                    <li>The new token will be automatically shared with this evaluation application</li>
                    <li>Try running your evaluation again</li>
                </ol>
            </div>
            
            <hr>
            <div class="mb-0">
                <a href="http://localhost:8080" target="_blank" class="btn btn-primary btn-sm me-2">
                    <i class="fas fa-external-link-alt me-1"></i>
                    Open Token Management
                </a>
                <button type="button" class="btn btn-outline-secondary btn-sm" data-bs-dismiss="alert">
                    <i class="fas fa-times me-1"></i>
                    Dismiss
                </button>
            </div>
        </div>
    `;
    
    // Insert at the top of the main container
    const mainContainer = document.querySelector('main.container') || document.querySelector('.container');
    if (mainContainer) {
        mainContainer.insertAdjacentHTML('afterbegin', alertHTML);
        
        // Scroll to the top to show the alert
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
    
    // Hide progress modal
    ProgressUI.hideProgress();
}
