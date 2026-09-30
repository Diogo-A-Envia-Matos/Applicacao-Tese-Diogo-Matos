# Dockerfile
FROM node:18

# Set the working directory
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

RUN npm install

# Copy the rest of the application to the working directory
COPY . .

# Ensure next command has execute permissions
RUN chmod +x node_modules/.bin/next

# Build the React app
RUN npm run build

# Expose port 3000
EXPOSE 3000

# Start the application
CMD ["npm", "start"]

